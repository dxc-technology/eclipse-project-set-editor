# DXC Eclipse Project Set Editor

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

> Project set files allow you to quickly import/export projects via Git, CVS and other Eclipse team providers.
>
> -- [https://wiki.eclipse.org/Project_Set_File](https://wiki.eclipse.org/Project_Set_File)


The **DXC Eclipse Project Set Editor** provides an editor for [project set files](https://wiki.eclipse.org/Project_set_file).

In addition it provides a specialized project set project (PS Project) with one [project set file](https://wiki.eclipse.org/Project_set_file).

PS projects enables the user to nest project sets by using project set projects as entries in other project sets. 
Project sets can then be loaded recursively.
If a PS project X is loaded, that contains a PS project Y, then the projects specified in the project set file in Y are also loaded.



## Build

If you want to bootstrap DXC Eclipse Project Set Editor, you'll need:
- Java 8+
- Maven 3.5.3 or later
- Run Maven:

```
mvn clean package
```

[If you are behind a proxy you must edit ./settings.xml](https://maven.apache.org/guides/mini/guide-proxies.html)

## Install

After you build DXC Eclipse Project Set Editor you can install it in Eclipse.

1. In Eclipse, choose **Help > Install New Software...**
2. In the "Work with" section, click the **Add...** button. The "Add Repository" dialog box appears.
3. Click **Local and** select the directory **./com.csc.dip.projectset-repository/target/repository** , then click **OK**. Its path appears in the "Location" field. Leave the "Name" field empty.
4. Select **Eclipse Project Set Editor**, then click **Next..**.
5. The **Install Details** dialog box appears. Select **ProjectSet**, then click **Next**.
6. Select **I accept the terms of the license agreement**, then click **Finish**.

## Documentation

See [doc/eclipse-project-set-editor.md](doc/project-set-editor.md)

## Contribute

See [CONTRIBUTING.md](CONTRIBUTING.md)




