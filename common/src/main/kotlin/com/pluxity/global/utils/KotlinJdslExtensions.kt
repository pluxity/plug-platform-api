package com.pluxity.global.utils

import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor

fun <T : Any> KotlinJdslJpqlExecutor.findAllNotNull(init: Jpql.() -> JpqlQueryable<SelectQuery<T>>): List<T> =
    this.findAll(init = init).filterNotNull()
