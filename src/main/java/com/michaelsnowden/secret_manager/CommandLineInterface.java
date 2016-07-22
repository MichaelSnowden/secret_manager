package com.michaelsnowden.secret_manager;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

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
            public void perform(SecretManager secretManager, String id, Map<String, String> map) {
                System.out.println(secretManager.listSecrets()
                        .stream()
                        .collect(Collectors.toList()));
            }
        },
        READ {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) {
                assert id != null;
                try {
                    secretManager.readSecret(id)
                            .forEach((k, v) -> System.out.println(k + " = " + v));
                } catch (AmazonS3Exception e) {
                    System.out.println(id + " does not exist");
                }
            }
        },
        WRITE {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) {
                assert id != null;
                Properties properties = new Properties();
                properties.putAll(map);
                secretManager.writeSecret(id, properties);
            }
        },
        DELETE {
            @Override
            public void perform(SecretManager secretManager, String id, Map<String, String> map) {
                assert id != null;
                secretManager.deleteSecret(id);
            }
        };

        public abstract void perform(SecretManager secretManager, String id, Map<String, String> map);
    }

    enum Storage {
        LOCAL {
            @Override
            public SecretManager getSecretManager() {
                return LocalSecretManager.localSecretManager();
            }
        },
        S3 {
            @Override
            public SecretManager getSecretManager() {
                return S3SecretManager.secretManager();
            }
        };

        public abstract SecretManager getSecretManager();
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
        try {
            SecretManager secretManager = storage.getSecretManager();
            action.perform(secretManager, id, map);
        } catch (CryptoKeyLengthException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        new CommandLineInterface().runWith(args);
    }
}