# Spark DataHub demo

## 1. Start DataHub

1. [Install Docker Desktop](https://www.docker.com/products/docker-desktop/)
2. Start Docker Desktop
3. Install Python 3.8+: [Download it from the official site](https://www.python.org/downloads/)
   or use [pyenv](https://github.com/pyenv/pyenv) to install it (recommended)
4. Verify python: `python3 --version` (version must be 3.8+)
5. [Install pipenv](https://pipenv.pypa.io/en/latest/installation.html): `pip3 install --user pipenv`
   (you may need to update your path)
6. Verify pipenv: `pipenv --version`
7. Create a new pipenv shell: `pipenv shell`
8. Install DataHub CLI
   ```shell
   pipenv install acryl-datahub
   datahub version
   ```
9. Start DataHub
   ```shell
   datahub docker quickstart
   ```
   This downloads a Docker Compose file to your home directory (`~/.datahub/quickstart`)
   and runs it to start all associated containers.
   > On Mac computers with Apple Silicon (M1, M2 etc.), you might see an error like `no matching manifest for
   > linux/arm64/v8 in the manifest list entries`. This typically means that the datahub cli was not able to
   > detect that you are running it on Apple Silicon. To resolve this issue, override the default architecture
   > detection by issuing `datahub docker quickstart --arch m1`
10. Sign In: navigate to the DataHub UI at http://localhost:9002 in your browser
    ```
    username: datahub
    password: datahub
    ```

🚀 That's it!

<details>

<summary>⚙️ Common Operations</summary> 

* Stop DataHub: `datahub docker quickstart --stop`
* Reset DataHub: `datahub docker nuke`
* Upgrade DataHub: `datahub docker quickstart`
* Back up DataHub: `datahub docker quickstart --backup`
* Restore DataHub: `datahub docker quickstart --restore`

</details>

## 2. Execute one of the Spark examples

This project contains 2 maven submodules. Both execute a Spark job locally and emit metadata to the respective metadata service.

* [datahub](datahub/README.md): Uses `DatahubSparkListener` to emit metadata in DataHub format
* [openlineage](openlineage/README.md): Uses `OpenLineageSparkListener` to emit metadata in OpenLineage format

Execute one of them by following the instructions specified in the respective README.
