# Building PlayerVaultPlus

## Prerequisites

- Java 21 or higher
- Gradle 8.5+ (or use the included Gradle Wrapper)

## Build Steps with Gradle

### Using Gradle Wrapper (Recommended)

1. Clone or download the project

2. Navigate to the project directory:
   ```bash
   cd PlayerVaultPlus
   ```

3. Build the project:
   ```bash
   ./gradlew build
   ```
   On Windows:
   ```bash
   gradlew.bat build
   ```

4. The compiled JAR will be in the `build/libs/` directory:
   ```
   build/libs/PlayerVaultPlus-1.2.0.jar
   ```

### Using Local Gradle Installation

If you have Gradle installed:

```bash
gradle build
```

## Installation

1. Copy `PlayerVaultPlus-1.2.0.jar` to your Paper server's `plugins/` folder

2. Restart your server

3. The plugin will create necessary directories automatically

## Troubleshooting

### Build Fails
- Ensure Java 21 is installed: `java -version`
- Delete `build` folder and rebuild: `./gradlew clean build`
- Clear Gradle cache: `./gradlew clean`

### Plugin Won't Load
- Check server logs for errors
- Verify plugin.yml is correct
- Ensure Paper version is 1.21.4 or higher
- Check that Java 21 is being used by the server

## Development

To work on the code:

1. Import the project into your IDE (IntelliJ IDEA, Eclipse, etc.)
2. IDEs will automatically detect the Gradle project and download dependencies
3. Make your changes
4. Rebuild: `./gradlew clean build`

## Gradle Tasks

Useful Gradle commands:

- `./gradlew build` - Build the project with shadow jar
- `./gradlew clean` - Remove build artifacts
- `./gradlew compileJava` - Only compile Java files
- `./gradlew shadowJar` - Create the fat JAR with shaded dependencies
- `./gradlew dependencies` - Show dependency tree
