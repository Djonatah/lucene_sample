public enum FileFields {
    CONTENT("content"), FILENAME("filename"), DATE("date"), SIZE ("size");

    String name;

    FileFields(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
