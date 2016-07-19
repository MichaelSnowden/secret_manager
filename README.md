# Secret Manager

This project provides a simple way to store and retrieve secrets (usually username/password credential pairs)

## Command line usage

- Clone this repository somewhere
- Create a properties file in "~/.secretManager/config.properties" like this for example
```
cryptoKey=0123456789ABCDEF # Any 16-character-long string will do
localSecretsFolder=~/.secretManager/secrets # Only necessary if using the local file system to store secrets
s3SecretsBucket=my-bucket # Only necessary if using S3 to store secrets
s3SecretsFolder=my-folder # Only necessary if using S3 to store secrets
```
- If storing passwords in S3, make sure your AWS credentials are stored on the computer by downloading the AWS CLI and running `aws configure`

```bash
$ cd /path/to/secret_manager
$ mvn clean compile assembly:single
$ java -jar target/secretManager.jar -a WRITE -s S3 -id GitHub -p username=michael -p password=password
```
- I recommend creating a bash script to make this a little easier and adding it to your `$PATH`
```bash
#!/usr/bin/env bash
java -jar /path/to/secret_manager/target/secretManager.jar "$@"
```
Then the command line usage would usually look something like this
```bash
$ secrets -s S3 -a LIST
[]
$ secrets -s S3 -a WRITE -id GitHub -p username=michael -p password=password -p something=else
$ secrets -s S3 -a LIST
[GitHub]
$ secrets -s S3 -a READ -id GitHub
username = michael
password = password
something = else
$ secrets -s S3 -a DELETE -id GitHub
$ secrets -s S3 -a LIST
[]
```

## Programmatic usage

- Add this to your pom.xml

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.MichaelSnowden</groupId>
        <artifactId>secret_manager</artifactId>
        <version>commit hash</version>
    </dependency>
</dependencies>
```

Then start by using either the `LocalSecretManger` or `S3SecretManger` class. 
You can also create your own secret manager by extending `SecretManager`.
You will simply have to provide a method for reading and writing bytes, as well as deleting and listing entries.
