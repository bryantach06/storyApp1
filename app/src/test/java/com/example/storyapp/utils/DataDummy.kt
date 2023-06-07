package com.example.storyapp.utils

import com.example.storyapp.responses.ListStoryItem

object DataDummy {
    fun generateDummyStories(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val list = ListStoryItem(
                "",
                "",
                "name: $i",
                "description: $i",
                "$i",
                -6.961833,
                107.567167,
            )
            items.add(list)
        }
        return items
    }
}