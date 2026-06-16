# Building PlayerVaultPlus

## Prerequisites

- Java 21 or higher
- Maven 3.6+

## Build Steps

1. Clone or download the project

2. Navigate to the project directory:
   ```bash
   cd PlayerVaultPlus
   ```

3. Build the project:
   ```bash
   mvn clean package
   ```

4. The compiled JAR will be in the `target/` directory:
   ```
   target/PlayerVaultPlus-1.0.0.jar
   ```

## Installation

1. Copy `PlayerVaultPlus-1.0.0.jar` to your Paper server's `plugins/` folder

2. Restart your server

3. The plugin will create necessary directories automatically

## Troubleshooting

### Build Fails
- Ensure Java 21 is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Delete `target` folder and rebuild: `mvn clean package`

### Plugin Won't Load
- Check server logs for errors
- Verify plugin.yml is correct
- Ensure Paper version is 1.21.4 or higher
- Check that Java 21 is being used by the server

## Development

To work on the code:

1. Import the project into your IDE (IntelliJ IDEA, Eclipse, etc.)
2. Run Maven to download dependencies: `mvn clean install`
3. Make your changes
4. Rebuild: `mvn clean package`
