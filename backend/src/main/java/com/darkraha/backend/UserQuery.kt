package com.darkraha.backend

import com.darkraha.backend.infos.*
import kotlin.reflect.KClass

interface UserQuery : ResultReader, ResultOptionsReader,
    ParamReader, ErrorReader,
    MetaInfoReader, Workflow,
    WorkflowState, WorkflowReader, WorkflowCancel {

    //-------------------------------------------------------------
    //  other

    fun getCommand(): String?

    fun getQueryId(): String?

    fun getCmdQueryId(): String?

    fun getComment(): String?

    // fun getChainType(): ChainType

}

inline fun <reified T : Service> UserQuery.getServiceAs(): T? {
    return service() as T?
}


inline fun <reified T> UserQuery.getResultAs(): T? {
    return result() as T?
}

inline fun <reified T> UserQuery.getSrcValueAs(): T? {
    return source().value as T?
}


inline fun UserQuery.getSourceValue(): Any? {
    return source().value
}

inline fun <reified T> UserQuery.getSourceClass(): KClass<*>? {
    return source().cls
}

inline fun UserQuery.getSourceItemClass(): KClass<*>? {
    return source().clsItem
}

inline fun UserQuery.getSourceMimetype(): String? {
    return source().mimetype
}


inline fun UserQuery.getDestinationValue(): Any? {
    return destination().value
}

inline fun UserQuery.getDestinationMimetype(): String? {
    return destination().mimetype;
}

inline fun UserQuery.getDestinationClass(): KClass<*>? {
    return destination().cls;
}

inline fun UserQuery.getDestinationItemClass(): KClass<*>? {
    return destination().clsItem;
}


inline fun <reified T> UserQuery.getDestValueAs(): T? {
    return destination().value as T?
}

inline fun <reified T> UserQuery.getExtraParamAs(): T? {
    return extraParam() as T?
}

inline fun <reified T> UserQuery.getUiParamAs(): T? {
    return uiParam() as T?
}

inline fun <reified T> UserQuery.getObjectParamAs(): T? {
    return objectParam() as T?
}

/**
 * This query is standalone (independent from other). So all user's callbacks methods must be invoked.
 */
inline fun UserQuery.isStandAloneQuery() = chainTypeCreate() == ChainType.STAND_ALONE

/**
 * This query first query of query's chain. So only error/canceling must be handled by user callbacks.
 */
inline fun UserQuery.isFirstQuery() = chainTypeCreate() == ChainType.FIRST_ELEMENT


/**
 * This query last of query's chain.  So all user's callbacks methods must be invoked, except prepare.
 */
inline fun UserQuery.isLastQuery() = chainTypeCreate() == ChainType.LAST_ELEMENT

