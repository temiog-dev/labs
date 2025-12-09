# Lab 5: Runtime Controls

## Learning Goals

- Understand Kosli Environments and how they track runtime state
- Create a Kosli Environment to represent your deployment target
- Snapshot a Docker environment to report what's running
- Understand Environment Snapshots and change tracking
- Create and configure compliance Policies
- Attach Policies to Environments for enforcement
- Integrate environment snapshotting into your CI/CD pipeline

## Introduction

**Kosli Environments** allow you to track what's actually running in your runtime environments (dev, staging, production, etc.). By taking regular **snapshots**, Kosli creates an immutable record of:

- What artifacts are running, identified by their digest (SHA256)
- When they started and stopped
- Whether they're compliant with your policies
- The complete change history over time

**Policies** define compliance requirements for environments. They specify rules like:

- All artifacts must have provenance (be part of a Flow)
- All artifacts must have passed specific tests
- All artifacts must have security scans
- Specific attestations must be present

Together, Environments and Policies provide runtime visibility and enforcement for your software supply chain.

### Understanding Environment Types

Kosli supports several environment types:

- **docker**: Tracks Docker containers on a host
- **k8s**: Tracks Kubernetes pods in namespaces
- **ECS**: Tracks AWS ECS tasks
- **Lambda**: Tracks AWS Lambda functions
- **S3**: Tracks files in S3 buckets
- **server**: Tracks files on a server filesystem, effectively making it possible to track any type of application runtime.

See [Environments documentation](https://docs.kosli.com/getting_started/environments/) for more details.

## Prerequisites

- Completed [Lab 4: Release Controls and Compliance](lab-04-release-controls.md)
- Kosli CLI installed and configured
- Artifacts and attestations flowing through your pipeline
- Understanding of your deployment process

## Exercise

### Overview

In this lab, you will:

- Create a Kosli Environment representing your deployment target
- Manually snapshot a Docker environment
- Understand how Kosli tracks environment changes
- Create a compliance Policy with requirements
- Attach the Policy to your Environment
- Integrate automated snapshotting into your workflow
- View environment snapshots and compliance status in Kosli

### Step-by-step instructions

#### Create an Environment

Your application deploys as a Docker container, so you'll create a `docker` type environment:

```bash
# Create a Docker environment
kosli create environment labs-prod \
  --type docker \
  --description "Production environment for labs application"

# Verify it was created
kosli get environment labs-prod
```

Visit [app.kosli.com](https://app.kosli.com), navigate to Environments, and you should see `labs-prod` listed (currently with no snapshots).

See [kosli create environment](https://docs.kosli.com/client_reference/kosli_create_environment/) for more details.

#### Explore an existing Environment

Since we cannot assume everyone has Docker installed locally, let's look at a real-world example of an environment in Kosli.

Navigate to the [Cyber-Dojo AWS Beta environment](https://app.kosli.com/cyber-dojo/environments/aws-beta/snapshots/).

Here you can see a history of snapshots taken from an AWS environment. Each snapshot shows exactly what was running at that point in time.

In the Kosli web interface you can see

  - **Running artifacts**: The container image(s) currently running.
  - **Compliance status**: Whether they meet policy requirements (if any)
  - **Events**: What started or stopped since the last snapshot
  - **Duration**: When this application started running on the environment.

Each snapshot is immutable. If you take another snapshot and nothing changed, Kosli won't create a new one. New snapshots are only created when there are actual changes.

> :bulb: Regular snapshotting creates a complete audit trail of your production environment, showing exactly what was running at any point in time. If nothing has changed, Kosli will not persist that snapshot.

#### Snapshot the environment in CI

Let's integrate automated snapshotting into your workflow by adding a step to the `Deploy` job in `.github/workflows/full-pipeline.yaml`.

Add this step after the "Deploy to production" step:

```yaml
    - name: Kosli snapshot environment
      run: kosli snapshot docker labs-prod 
```

> :bulb: In your workflow, this runs after the application is deployed. It captures what's running immediately after deployment.

#### Create a compliance Policy

Policies define what's required for artifacts to be compliant. Let's create a policy:

**Create `.kosli-policy.yml`:**

```yaml
_schema: https://kosli.com/schemas/policy/environment/v1

artifacts:
  provenance:
    required: true  # All artifacts must be part of a Flow
  
  attestations:
    - name: unit-tests  # Must have unit test attestation
      type: junit
    - name: sbom  # Must have SBOM attestation
      type: "*"  # Any attestation type
```

This policy requires:
1. All running artifacts must have been attested to Kosli (provenance)
2. All artifacts must have JUnit test results
3. All artifacts must have an SBOM

Create the policy in Kosli:

```bash
kosli create policy labs-prod-requirements .kosli-policy.yml

# View the policy
kosli get policy labs-prod-requirements
```

See [kosli create policy](https://docs.kosli.com/client_reference/kosli_create_policy/) and [Policy documentation](https://docs.kosli.com/getting_started/policies/) for more details.

#### Attach the Policy to your Environment

Now attach the policy to to the environment to activate it:

```bash
kosli attach-policy labs-prod-requirements --environment labs-prod

# Verify attachment
kosli get environment labs-prod
```

Attaching the policy automatically triggers a new snapshot evaluation.  Kosli will check if currently running artifacts meet the policy requirements.

See [kosli attach policy](https://docs.kosli.com/client_reference/kosli_attach_policy/) for more details.

#### View compliance status

Return to the Kosli web interface:

1. Navigate to Environments → labs-prod
2. Look at the latest snapshot
3. Check the compliance status:
   - **Compliant**: Green - all requirements met
   - **Non-compliant**: Red - some requirements not met
4. Click on artifacts to see which attestations are present

<details>
<summary>:bulb: If your artifact is non-compliant, it might be because:</summary>

- The attestation names in your scripts don't match the policy
- Some attestations are missing
- The artifact wasn't properly attested in the first place

</details>

> :bulb: Policies towards environments are evaluated on every snapshot. If you want to have it as a gate-keeper before a deployment, use the `kosli assert` command, discussed further in `Policy enforcement gate` section.

#### Manage policy in CI

Let's automate policy updates by adding a step to your workflow. You can add this to the `Deploy` job, before the deployment happens.

Add this step before the "Assert Compliance" step:

```yaml
    - name: Update Kosli policy
      run: kosli create policy labs-prod-requirements .kosli-policy.yml
    - name: Attach policy to environment
      run: kosli attach policy labs-prod-requirements --environment labs-prod
```

#### Verify workflow integration

Ensure your `.github/workflows/full-pipeline.yaml` now includes the policy update and snapshot steps in the `Deploy` job.

#### Test the complete integration

1. Commit the changes:

```bash
# Commit policy and workflow
git add .kosli-policy.yml .github/workflows/full-pipeline.yaml
git commit -m "Add Kosli environment and policy management"
git push origin main
```

2. Watch the workflow execute
3. Verify the "Update policy" and "Kosli snapshot environment" steps succeed
4. In Kosli web interface:
   - Check your environment has a new snapshot
   - Verify the running artifact's compliance status
   - Review which attestations are present

> :bulb: Your artifact should now be compliant since you've been attesting unit tests and SBOM in previous labs.

#### Optional: Policy enforcement gate

You can use policies as deployment gates to prevent non-compliant artifacts from being deployed:

```bash
# Check if an artifact is compliant before deployment
kosli assert artifact ghcr.io/${IMAGE}:latest \
  --environment labs-prod \
  --fingerprint $(kosli fingerprint docker ghcr.io/${IMAGE}:latest)

# Or assert against specific policies
kosli assert artifact ghcr.io/${IMAGE}:latest \
  --policy kosli-prod-requirements
```

If the artifact is non-compliant, this command exits with a non-zero status, failing your deployment.

See [kosli assert artifact](https://docs.kosli.com/client_reference/kosli_assert_artifact/) for more details.

#### Understanding policy expressions

Policies support conditional logic using expressions. For example:

```yaml
artifacts:
  attestations:
    # Only require security scans for production flow
    - if: ${{ flow.name == "production" }}
      name: security-scan
      type: snyk
    
    # Exceptions: don't require tests for specific artifacts
    - name: unit-tests
      type: junit
      exceptions:
        - if: ${{ artifact.name == "legacy-component" }}
```

> :bulb: Policy expressions allow sophisticated compliance rules based on flow tags, artifact names, and other metadata.

See [Policy expressions](https://docs.kosli.com/getting_started/policies/#policy-expressions) for more details.

### Verification Checklist

Before completing this lab, ensure you have:

- ✅ Created a `labs-prod` environment of type `docker`
- ✅ Explored the Cyber-Dojo environment in Kosli
- ✅ Created a `.kosli-policy.yml` file with compliance requirements
- ✅ Created the policy in Kosli and attached it to your environment
- ✅ Updated workflow with policy update and snapshot steps
- ✅ Workflow runs successfully with policy updates and snapshotting
- ✅ Can see environment snapshots in the Kosli web interface
- ✅ Can see compliance status (compliant/non-compliant) for running artifacts
- ✅ Understand how policies enforce requirements

### Clean up

No cleanup is required. Environments, snapshots, and policies remain for audit and historical tracking.

## Conclusion

Congratulations! You've completed the Kosli learning labs. You now know how to:

1. Set up Kosli and integrate it with your CI/CD pipeline
2. Create Flows and Trails to track your software delivery process
3. Attest artifacts and attach evidence (tests, SBOMs, scans)
4. Create environments and track what's running
5. Define and enforce compliance policies

You have established complete visibility and control over your software supply chain, from build to deployment.

## Next Steps

- Explore [custom attestation types](https://docs.kosli.com/getting_started/attestations/#custom) for your specific tools
- Set up [Kubernetes environment reporting](https://docs.kosli.com/tutorials/report_k8s_envs) if you use K8s
- Implement [approval workflows](https://docs.kosli.com/getting_started/approvals/) for production deployments
- Use [Kosli CLI in pre-commit hooks](https://docs.kosli.com/integrations/git_hooks) for local validation
- Explore the [Kosli API](https://docs.kosli.com/api_reference/) for custom integrations

## Resources

- [Kosli Environments Documentation](https://docs.kosli.com/getting_started/environments/)
- [Kosli Policies Documentation](https://docs.kosli.com/getting_started/policies/)
- [kosli create environment CLI Reference](https://docs.kosli.com/client_reference/kosli_create_environment/)
- [kosli snapshot docker CLI Reference](https://docs.kosli.com/client_reference/kosli_snapshot_docker/)
- [kosli create policy CLI Reference](https://docs.kosli.com/client_reference/kosli_create_policy/)
- [kosli attach policy CLI Reference](https://docs.kosli.com/client_reference/kosli_attach_policy/)
- [kosli assert artifact CLI Reference](https://docs.kosli.com/client_reference/kosli_assert_artifact/)
- [Policy Schema Reference](https://docs.kosli.com/template_ref/)
