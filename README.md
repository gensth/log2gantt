# ![log2gantt logo](https://raw.github.com/gensth/log2gantt/master/images/logo_64.png) log2gantt

creates gantt charts to visualize the time and duration of login sessions on a linux/unix system.

Example:  
![log2gantt screenshot](https://raw.github.com/gensth/log2gantt/master/images/auth_log_gantt.png)

<!--
Project Labels: security monitoring linux unix login session gantt visualize image cron java command visualization logcheck
-->

## Features

*   parses log files as `/var/log/auth.log`
*   creates a gantt chart with a bar for each user showing the time and duration of his/her logins
*   output as png or jpeg

## Requirements

*   Java 1.5 or later

## License

The content of this project is licensed under the [GNU Lesser GPL](https://github.com/gensth/log2gantt/blob/master/license-LGPL.txt).

## Quickstart

*   download the current version of `log2gantt-*.jar`
*   execute on the command line:  
    `java -jar log2gantt-*.jar -i /var/log/auth.log`)
*   eventually include it in a [cron job](#deploy_logcheck) and send the output image by mail to the server admin

## Usage

See the full list of command line arguments by executing `java -jar log2gantt-*.jar --help`

```
usage: java log2gantt.AuthLog2Gantt [options]<br>
where options include:<br>
-i logfile        the input logfile to parse (default: auth.log)<br>
-o imagefile      the output image file to write (default: auth_log_gantt.png)<br>
-w width          the width of the output image (default: 600) [pixels]<br>
-h height         the height of the output image (default: 0) [pixels]<br>
-t title          the title in the output image (default: null)<br>
-f                to force overwriting the output file (default: false)<br>
```

<a name="deploy_logcheck"></a>
## Integrating log2gantt into Logcheck

For a daily update about what's happening on our server we integrated _log2gantt_ in the [Logcheck](http://logcheck.org) script which is executed daily by cron.

In the script we call _log2gantt_ after the file `auth_check.$$` was created by inserting the following lines

```
JARFILE=$(ls -1 /usr/local/log2gantt/log2gantt-*.jar | sort | tail -n 1)
java -Xmx30m -jar $JARFILE -i $TMPDIR/auth_check.$$ -o $TMPDIR/auth_gantt_$$.png -f
```

To attach the gantt graph picture to the result mail we inserted `-a /$TMPDIR/gantt_chart_$$.png` after the two `$MAIL` commands at the end of the script (we use `mutt` as mailer).

Finally we let cron execute the Logcheck script daily by executing the command

```
> ln -s /usr/local/bin/logcheck.sh /etc/cron.daily/logcheck.sh
```
