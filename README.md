# Whipper

Whipper is database query regression testing tool to be used with any relational datasources like Oracle, MySQL, Postgres and especially with Teiid. The user can define set of queries and expected results, and this tool can efficiently run and provide the results.

The important aspect this can be built into CI/CD development model, where a Jenkins job can be setup to run automatically in your environment. 

For any issues please log [JIRA](https://issues.jboss.org/browse/TEIIDWHIP)

# Usage

TODO

# How to contribute

There is a simple way how to contribute:

1. Fork the project
2. Implement your new cool feature in a new branch (in forked project)
3. Squash all commits into one commit
4. Create a pull request

There are some rules you should follow while implementing new feature:

1. **Use 4 spaces as an indentation, not tabs.**  
  We agree that there are no clear arguments for using spaces or tabs for indentation.
  But we hope we all agree that they should not be mixed on any level (project, file, line).
  In this project, we decided to use spaces, so please respect it.
2. **`org.whipper.xml.[result|error|suite]` packages.**  
  Classes in those packages are generated automatically from XSD schemas. Do not bother to
  changed them, *git* will ignore them.
  If you need perform any changes in those files, update respective XSD schema.
  By default, those classes are generated in `generate-sources` maven lifecycle. So after you
  clone the project you might want to run `mvn generate-sources` before you start fixing
  imports in classes :-).
3. **Avoid any copy&paste**  
  This is a bad idea in general. If you need to copy&paste some code, think whether you can re-use
  the original one.
4. **Do not modify lines if you do not have to**  
  This usually happens if you use formatting utility in your IDE on whole file, or if git is configured
  to change end of the lines on commit. Please, try to avoid it. It breaks history in git (i.e. `git blame`)
  and it's harder to review your commit.
5. **Avoid multi-commit pull requests**  
  It is much simpler to review one commit then several commits
