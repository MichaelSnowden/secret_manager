# Secret Manager

This project provides a simple way to store and retrieve secrets (usually username/password credential pairs)

## Usage

- Clone this repository somewhere
- Create a properties file in "~/.secretManager/config.properties" like this for example
```
encryptionKey=0123456789ABCDEF # Any 16-character-long string will do
localSecretsFolder=~/.secretManager/secrets # Only necessary if using the local file system to store secrets
s3SecretsBucket=my-bucket # Only necessary if using S3 to store secrets
s3SecretsFolder=my-folder # Only necessary if using S3 to store secrets
```
- If storing passwords in S3, make sure your AWS credentials are stored on the computer by downloading the AWS CLI and running `aws configure`

```bash
$ cd /path/to/secret_manager
$ mvn clean compile assembly:single
$ java -jar target/secretManager.jar -a WRITE -s S3 -p username=michael -p password=password
```
- I recommend creating a bash script to make this a little easier and adding it to your `$PATH`
```bash
#!/usr/bin/env bash
java -jar /path/to/secret_manager/target/secretManager.jar "$@"
```