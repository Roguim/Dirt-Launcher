When running from a idea application , make sure to set the default runner to gradle not idea. https://stackoverflow.com/questions/38665255/how-to-use-gradle-run-with-intellij-idea
Alternatively, you can just use the JavaFX>JfxJar task rather then run it using an application from IDEA.
this allows the tokens to replace and whatnot which may cause issues if it is not ran correctly.