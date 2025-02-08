# 🚀 Contributing to SNMP Browser

Thank you for your interest in contributing to this project! We welcome contributions from everyone, whether it's fixing bugs, improving documentation, or adding new features. This guide will help you get started.

---
## 📜 Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](https://github.com/chutrunganh/Project-I-Collect-SNMP-Data/blob/master/.github/CODE_OF_CONDUCT.md). Please read it before making any contributions. 🙌

## 🤝 How to Contribute

🐛 **Reporting Bugs**

- **Check Existing Issues**: Before reporting a bug, please check the  to ensure it hasn’t already been reported.

- **Create a New Issue**: If the bug hasn’t been reported, open a new issue and provide the following details:

  - A clear and descriptive title.
  
  - Steps to reproduce the issue.
  
  - Expected vs. actual behavior.
  
  - Screenshots, logs, or error messages (if applicable).
  
  - Your environment (e.g., OS, browser, version).

💡 **Suggesting Enhancements**

- **Check Existing Discussions**: Look through the [Issues page](https://github.com/chutrunganh/Project-I-Collect-SNMP-Data/issues) to see if your enhancement has already been suggested.

- **Open a New Issue**: If not, create a new issue and include:

  - A clear and descriptive title.

  - A detailed explanation of the enhancement.

  - Why this enhancement would be useful.

  - Examples or references (if applicable).

📤 **Submitting Pull Requests**

1. **Fork the Repository**
   
    Start by forking the repository to your GitHub account.

2. **Clone the Repository**

   Clone the forked repository to your local machine:

    ```bash
    git clone https://github.com/chutrunganh/Project-I-Collect-SNMP-Data.git
    ```

3. **Create a Branch**

    Create a new branch for your feature or bug fix:

    ```bash
    git checkout -b feature/your-feature-name
    ```

4. **Make Changes**

    Make your changes to the codebase. Ensure your code follows the project's coding standards and conventions.

5. **Test Your Changes**

    Ensure your changes work as expected and do not introduce new issues.

6. **Commit Changes**

     Commit your changes with a clear and concise commit message:

    ```bash
    git add .
    git commit -m "Add feature: your feature name"
    ```

7. **Push Changes**

     Push your changes to your forked repository:

    ```bash
    git push origin feature/your-feature-name
    ```

8. **Create a Pull Request**

    Go to the original repository on GitHub and create a pull request from your forked repository. 

    Provide a clear title and description for your PR, including:

    - The purpose of the changes.
    
    - Any related issues (e.g., "Fixes #123").
    
    - Screenshots or test results (if applicable)

---
## ⚒️ Development Setup

To set up the project locally, follow these steps:

1. **Clone the repository**
    ```bash
    git clone https://github.com/chutrunganh/Project-I-Collect-SNMP-Data.git
    ```
2. **Install dependencies**

- Ensure you have Java SDK (version 21.0.3 or later) and JavaFX installed to run the application.
  Use the following VM options to run the application:  ```--module-path Path_To_JavaFX/lib --add-modules javafx.controls,javafx.fxml```.
  
  
- Additional dependencies:
  - fasterxml.jackson.core.databind for reading JSON files
  - snmp4j for SNMP operations
  - pysnmp-pysmi for converting .mib files to JSON (only needed if you intend to recompile MIB files)

  All dependencies are included in the `pom.xml` file. You can install them automatically using Maven.


3. **Run the application**

    Run the `Main.java` in the `src/main/java/Main.java` directory to start the application.

 

---

## 🎨 Style Guidelines

- **Code Formatting**: Follow the existing code style (e.g., indentation, naming conventions).

- **Documentation**: Update documentation (e.g., README, comments) to reflect your changes.

- **Testing**: Write unit tests for new features or bug fixes.
---
## ❓ Questions or Need Help?

If you have any questions or need assistance, feel free to:

- Open an [Issue](https://github.com/chutrunganh/Project-I-Collect-SNMP-Data/issues/new?template=Blank+issue).

- Reach out to us via this [Email](mailto:chutrunganh04@gmail.com).

---

We appreciate your contributions and look forward to collaborating with you!  🎉 🎉 🎉
