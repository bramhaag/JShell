# JShell for Spigot
JShell is Java's REPL (read-eval-print loop) tool introduced in JDK 9. With this plugin you can use JShell in Minecraft!

**Warning: Due to obvious security risks this plugin should only ever be used in testing environments!**
 
View on spigot: https://www.spigotmc.org/resources/jshell.47753/

### Requirements
- JDK >=14 (**NOT** JRE!)
- Spigot (or a fork) >=1.16

### Installation
- Place the plugin in the `plugins/` directory
- Run your server with JDK
- (Optional) Define default imports and variables in the `config.yml` file

### Commands
 - `/jshell [code]` - Create or resume a JShell session. Optionally you can instantly run code by using the `[code]` argument.
 - `/exit` - Exit the JShell session.
 - `/variables` - See all variables from your session.
 - `/imports` - See all imports from your session.
 - `/clear` - Exit and clear the JShell session. This will remove all imports and variables.



