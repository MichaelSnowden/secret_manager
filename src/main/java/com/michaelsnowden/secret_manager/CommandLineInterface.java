package com.michaelsnowden.secret_manager;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Created by michael.snowden on 7/17/16.
 */
public class CommandLineInterface {
    enum Action {
        LIST {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) throws SecretManagerException {
                System.out.println(secretManager.listSecrets()
                        .stream()
                        .collect(Collectors.toList()));
            }
        },
        READ {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) throws SecretManagerException {
                assert id != null;
                secretManager.readSecret(id)
                        .forEach((k, v) -> System.out.println(k + " = " + v));
            }
        },
        WRITE {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) throws SecretManagerException {
                assert id != null;
                Properties properties = new Properties();
                properties.putAll(map);
                secretManager.writeSecret(id, properties);
            }
        },
        DELETE {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) throws SecretManagerException {
                assert id != null;
                secretManager.deleteSecret(id);
            }
        };

        public abstract void perform(SecretManager secretManager, String id, Map<String, String> map) throws Exception;
    }

    enum Storage {
        LOCAL {
            @Override
            public SecretManager getSecretManager(Properties config) throws CryptoKeyLengthException {
                String cryptoKey = config.getProperty("cryptoKey");
                if (cryptoKey.length() != 16) {
                    throw new CryptoKeyLengthException(cryptoKey);
                }
                return new LocalSecretManager(cryptoKey
                        .getBytes(StandardCharsets.UTF_8), new File(config.getProperty("localSecretsFolder")));
            }
        },
        S3 {
            @Override
            public SecretManager getSecretManager(Properties config) throws CryptoKeyLengthException {
                String cryptoKey = config.getProperty("cryptoKey");
                if (cryptoKey.length() != 16) {
                    throw new CryptoKeyLengthException(cryptoKey);
                }
                return new S3SecretManager(cryptoKey
                        .getBytes(StandardCharsets.UTF_8), config.getProperty("s3SecretsBucket"), config.getProperty("s3SecretsFolder"), new AmazonS3Client(new ProfileCredentialsProvider()));
            }
        };

        public abstract SecretManager getSecretManager(Properties config) throws CryptoKeyLengthException;
    }

    @Option(name = "-a", usage = "The action  to take", required = true)
    Action action;

    @Option(name = "-s", usage = "The storage to use (either local file system or S3)", required = true)
    Storage storage;

    @Option(name = "-id", usage = "The id of the file to either read or delete")
    String id;

    @Option(name = "-p", usage = "Any property to store in the secret file (i.e. -p uname=alice)")
    private Map<String, String> map;

    public void runWith(String[] args) throws Exception {
        CmdLineParser cmdLineParser = new CmdLineParser(this);
        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            System.out.println("An error occurred while parsing the arguments.");
            System.out.println("Please refer to the below usage and try again.");
            cmdLineParser.printUsage(System.out);
            return;
        }
        File secretManagerDirectory = new File(System.getProperty("user.home"), ".secretManager");
        Properties config = new Properties();
        File configFile = new File(secretManagerDirectory, "config.properties");
        try {
            config.load(new FileInputStream(configFile));
        } catch (FileNotFoundException e) {
            System.out.println("Please make sure that the config file is located at " + configFile.getAbsolutePath());
            System.out.println("Here, you should set the 'encryptionKey' to a 16-character-long string");
            System.out.println("You should also set 'localSecretsFolder' or 's3SecretsBucket' and 's3SecretsFolder' depending on whether you are using S3 or the local file system to store secrets");
        }
        try {
            SecretManager secretManager = storage.getSecretManager(config);
            action.perform(secretManager, id, map);
        } catch (CryptoKeyLengthException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        new CommandLineInterface().runWith(args);
    }
}