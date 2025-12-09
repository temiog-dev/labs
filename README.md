# Kosli Learning Labs

This repository contains hands-on learning labs to help DevOps engineers, Platform engineers, and software developers get familiar with [Kosli](https://kosli.com) - a DevOps platform for software supply chain visibility and compliance.

## About These Labs

These labs provide a progressive, practical introduction to Kosli's core features. You'll learn how to track your software delivery process from build through deployment, establish compliance requirements, and maintain complete visibility into your software supply chain.

Each lab builds on the previous one, taking you from initial setup through runtime compliance enforcement.

## Prerequisites

- A GitHub account
- Basic familiarity with Git, CI/CD concepts, and command line
- No prior Kosli experience required

## Lab Structure

### [Lab 1: Get Ready](lab-01-get-ready.md)

Set up your Kosli account and verify the sample application pipeline.

**You'll learn:**

- Creating a Kosli account and organization
- Creating a copy of the sample repository
- Understanding the existing CI/CD pipeline

### [Lab 2: Flows and Trails](lab-02-flows-and-trails.md)

Install the Kosli CLI and start tracking your delivery process.

**You'll learn:**

- Installing and configuring the Kosli CLI
- Creating Flows to represent your CI/CD pipeline
- Beginning Trails to track individual executions
- Integrating Kosli into your GitHub Actions workflow

### [Lab 3: Build Controls and Attestations](lab-03-build-controls.md)

Record evidence about your artifacts and build process.

**You'll learn:**

- Attesting artifacts (binaries and Docker images)
- Attaching JUnit test results
- Generating and attesting Software Bill of Materials (SBOM)
- Understanding the attestation audit trail

### [Lab 5: Runtime Controls](lab-05-runtime-controls.md)

Track what's running in production and enforce compliance policies.

**You'll learn:**
- Creating Kosli Environments
- Snapshotting Docker environments
- Defining compliance policies
- Enforcing policy requirements
- Viewing compliance status

## Getting Started

Start with [Lab 1: Get Ready](lab-01-get-ready.md) and work through the labs sequentially. Each lab includes:

- Clear learning objectives
- Step-by-step instructions
- Verification checklists
- Links to relevant Kosli documentation

## Resources

- [Kosli Documentation](https://docs.kosli.com/)
- [Kosli Getting Started Guide](https://docs.kosli.com/getting_started/)
- [Kosli CLI Reference](https://docs.kosli.com/client_reference/)
- [Kosli Website](https://kosli.com)

## Support

If you encounter issues or have questions:

- Check the [Kosli Documentation](https://docs.kosli.com/)
- Review the verification checklist in each lab
- Contact [Kosli Support](https://www.kosli.com/contact/)

## License

See [LICENSE](LICENSE) file for details.
