package com.project.algo.domain.enums;

public enum ProgrammingLanguage {

    PYTHON("Python",         "python",     ".py",   "python"),
    JAVA("Java",             "java",       ".java", "java"),
    CPP("C++",               "cpp",        ".cpp",  "c++"),
    C("C",                   "c",          ".c",    "c"),
    JAVASCRIPT("JavaScript", "javascript", ".js",   "javascript"),
    TYPESCRIPT("TypeScript", "typescript", ".ts",   "typescript"),
    GO("Go",                 "go",         ".go",   "go"),
    KOTLIN("Kotlin",         "kotlin",     ".kt",   "kotlin"),
    SWIFT("Swift",           "swift",      ".swift","swift"),
    RUST("Rust",             "rust",       ".rs",   "rust"),
    RUBY("Ruby",             "ruby",       ".rb",   "ruby");

    private final String displayName;
    private final String syntaxMode;
    private final String fileExtension;
    /** Piston API가 인식하는 언어 식별자 (C++은 "c++"로 달라서 별도 관리) */
    private final String pistonLanguage;

    ProgrammingLanguage(String displayName, String syntaxMode, String fileExtension, String pistonLanguage) {
        this.displayName    = displayName;
        this.syntaxMode     = syntaxMode;
        this.fileExtension  = fileExtension;
        this.pistonLanguage = pistonLanguage;
    }

    public String getDisplayName()    { return displayName; }
    public String getSyntaxMode()     { return syntaxMode; }
    public String getFileExtension()  { return fileExtension; }
    public String getPistonLanguage() { return pistonLanguage; }
}
