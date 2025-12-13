plugins {
    java
    application
}

application {
    mainClass.set("com.example.Client"); 
}

dependencies {
    implementation(project(":core"))
}

