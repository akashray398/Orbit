package com.orbit.app.core.data.mapper

import com.orbit.app.core.data.database.entities.ActionLogEntity
import com.orbit.app.core.data.database.entities.ConversationEntity
import com.orbit.app.core.data.database.entities.MessageEntity
import com.orbit.app.core.data.database.entities.NoteEntity
import com.orbit.app.core.data.database.entities.ReminderEntity
import com.orbit.app.core.data.database.entities.TaskEntity
import com.orbit.app.core.domain.model.ActionLog
import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.Conversation
import com.orbit.app.core.domain.model.Note
import com.orbit.app.core.domain.model.Reminder
import com.orbit.app.core.domain.model.Task

fun ConversationEntity.toDomain(): Conversation = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    archived = archived
)

fun Conversation.toEntity(): ConversationEntity = ConversationEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    archived = archived
)

fun MessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    role = role,
    content = content,
    timestamp = timestamp,
    status = status,
    actionToolLabel = actionToolLabel
)

fun ChatMessage.toEntity(conversationId: String = "default_conversation"): MessageEntity = MessageEntity(
    id = id,
    conversationId = conversationId,
    role = role,
    content = content,
    status = status,
    timestamp = timestamp,
    actionToolLabel = actionToolLabel
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinned = pinned,
    source = source,
    tags = tags
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinned = pinned,
    source = source,
    tags = tags
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    notes = notes,
    dueAt = dueAt,
    priority = priority,
    status = status,
    completedAt = completedAt,
    reminderId = reminderId,
    createdAt = createdAt,
    source = source
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    notes = notes,
    dueAt = dueAt,
    priority = priority,
    status = status,
    completedAt = completedAt,
    reminderId = reminderId,
    createdAt = createdAt,
    source = source
)

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    title = title,
    triggerAt = triggerAt,
    repeatRule = repeatRule,
    workRequestId = workRequestId,
    status = status,
    createdAt = createdAt,
    source = source
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    title = title,
    triggerAt = triggerAt,
    repeatRule = repeatRule,
    workRequestId = workRequestId,
    status = status,
    createdAt = createdAt,
    source = source
)

fun ActionLogEntity.toDomain(): ActionLog = ActionLog(
    id = id,
    toolName = toolName,
    argumentsJson = argumentsJson,
    resultSummary = resultSummary,
    status = status,
    riskLevel = riskLevel,
    startedAt = startedAt,
    finishedAt = finishedAt,
    conversationId = conversationId,
    messageId = messageId,
    undoPayloadJson = undoPayloadJson,
    undoneAt = undoneAt
)

fun ActionLog.toEntity(): ActionLogEntity = ActionLogEntity(
    id = id,
    toolName = toolName,
    argumentsJson = argumentsJson,
    resultSummary = resultSummary,
    status = status,
    riskLevel = riskLevel,
    startedAt = startedAt,
    finishedAt = finishedAt,
    conversationId = conversationId,
    messageId = messageId,
    undoPayloadJson = undoPayloadJson,
    undoneAt = undoneAt
)
