package com.example.myquizbite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myquizbite.data.model.Topic

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val iconName: String = "code"
) {
    fun toModel() = Topic(id = id, name = name, description = description, iconName = iconName)
    companion object {
        fun from(topic: Topic) = TopicEntity(
            id = topic.id,
            name = topic.name,
            description = topic.description,
            iconName = topic.iconName
        )
    }
}
