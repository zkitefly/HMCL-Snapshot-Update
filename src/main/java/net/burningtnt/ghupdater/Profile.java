package net.burningtnt.ghupdater;

public final class Profile {
    private Profile() {
    }

    private String token;

    private String owner;

    private String repository;

    private String branch;

    private String workflowID;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private final Profile instance = new Profile();

        private Builder() {
        }

        public Builder setToken(String token) {
            instance.token = token;
            return this;
        }

        public Builder setOwner(String owner) {
            instance.owner = owner;
            return this;
        }

        public Builder setRepository(String repository) {
            instance.repository = repository;
            return this;
        }

        public Builder setBranch(String branch) {
            instance.branch = branch;
            return this;
        }

        public Builder setWorkflowID(String workflowID) {
            instance.workflowID = workflowID;
            return this;
        }

        public Profile build() {
            return instance;
        }
    }

    public String getToken() {
        return this.token;
    }

    public String getOwner() {
        return this.owner;
    }

    public String getRepository() {
        return this.repository;
    }

    public String getBranch() {
        return this.branch;
    }

    public String getWorkflowID() {
        return this.workflowID;
    }
}
