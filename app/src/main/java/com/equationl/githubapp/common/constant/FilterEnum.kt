package com.equationl.githubapp.common.constant

enum class LanguageFilter(val showName: String, val requestValue: String) {
    All("全部", " "),
    Java("Java", "Java"),
    Kotlin("Kotlin", "Kotlin"),
    Dart("Dart", "Dart"),
    ObjectiveC("Objective-C", "Objective-C"),
    Swift("Swift", "Swift"),
    JavaScript("JavaScript", "JavaScript"),
    PHP("PHP", "PHP"),
    Go("Go", "Go"),
    CPP("C++", "C++"),
    C("C", "C"),
    HTML("HTML", "HTML"),
    CSS("CSS", "CSS"),
}

enum class OrderFilter(val showName: String, val requestValue: String) {
    DESC("降序", "desc"),
    ASC("升序", "asc")
}

enum class TypeFilter(val showName: String, val requestValue: String) {
    BestMatch("最匹配", "best%20match"),
    Star("Star", "stars"),
    Fork("Fork", "forks"),
    Update("更新", "updated"),
}