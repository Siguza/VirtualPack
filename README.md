# VirtualPack

Bukkit Plugin "VirtualPack".

* [Bukkit page](http://dev.bukkit.org/bukkit-plugins/virtualpack/)
* [Dev builds (Jenkins)](https://ci.siguza.net/job/VirtualPack/)

### Building

Because VirtualPack (still) builds against CraftBukkit code (and I haven't managed to change that yet), and [CraftBukkit builds are no longer available](https://web.archive.org/web/20150206000441/http://dl.bukkit.org/dmca/notification.txt), you'll have to bring your own server jar.  

Steps to build:

0.&#32;Have `mvn` all set up.
1.&#32;`git clone https://github.com/Siguza/VirtualPack`  
2.&#32;Determine which version folder to use. The folders are named after the **first** supported version,  
&nbsp; &nbsp; &nbsp;so if you want to compile for 1.8.6 and there are only folders for 1.8.8 and 1.8.4, use the 1.8.4 one.  
3.&#32;Copy your server jar to `VirtualPack/<version>/lib/spigot.jar`.  
4.&#32;`cd VirtualPack/<version> && mvn`
