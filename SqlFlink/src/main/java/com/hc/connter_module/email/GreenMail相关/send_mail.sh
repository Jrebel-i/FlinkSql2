#!/usr/bin/env bash

send_from="jane"
send_to="jon"
subject="Example Message"
message="This is an example message"

while getopts f:t:s:m: option
do
    case "${option}" in
        f) send_from="${OPTARG}" ;;
        t) send_to="${OPTARG}" ;;
        s) subject="${OPTARG}" ;;
        m) message="${OPTARG}" ;;
    esac
done

password="${send_from}"
echo "${message}" | mailx \
  -Sv15-compat \
  -Smta="smtp://${send_from}:${password}@localhost:3025" \
  -s"${subject}" \
  "${send_to}@acme.org"


send_mail.sh -f bob -t alice -s "Subject" -m "Message"

echo "This is the email body" | mailx -Sv15-compat \
        -s"Email Subject" \
        -Smta="smtp://alice:alice@localhost:3025" \
        bob@acme.org

#com.hc.connter_module.email.imap.ImapSourceFactory
#com.hc.connter_module.email.imap.ImapCatalogFactory
#com.hc.connter_module.email.smtp.SmtpSinkFactory


启动配置greenmail服务
https://greenmail-mail-test.github.io/greenmail/#deployment

wget https://repo1.maven.org/maven2/com/icegreen/greenmail-standalone/1.6.7/greenmail-standalone-1.6.7.jar

java -Dgreenmail.setup.test.all \
     -Dgreenmail.users=alice:alice@acme.org,bob:bob@acme.org \
     -Dgreenmail.hostname=0.0.0.0 \
     -jar greenmail-standalone-1.6.7.jar

向bob@acme.org发送信息
echo "This is the email body" | mailx -Sv15-compat \
        -s"Email Subject" \
        -Smta="smtp://alice:alice@localhost:3025" \
        bob@acme.org