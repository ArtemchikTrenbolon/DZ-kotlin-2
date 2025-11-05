package com.example.gallery.model

data class Picture(
    val id: Int,
    val author: String,
    val url: String
)

fun generateSamplePictures(): List<Picture> = listOf(
    Picture(1, "Alice", "https://commons.wikimedia.org/wiki/Special:FilePath/Cat03.jpg"),
    Picture(2, "Bob",   "https://commons.wikimedia.org/wiki/Special:FilePath/Red_Kitten_01.jpg"),
    Picture(3, "Carol", "https://commons.wikimedia.org/wiki/Special:FilePath/June_odd-eyed-cat.jpg"),
    Picture(4, "Dave",  "https://commons.wikimedia.org/wiki/Special:FilePath/Cat_poster_1.jpg"),
    Picture(5, "Erin",  "https://commons.wikimedia.org/wiki/Special:FilePath/Siam_lilacpoint.jpg"),
)

