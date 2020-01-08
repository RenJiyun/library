package io.pillopl.library.lending.patron.model;

import io.vavr.control.Option;

/**
 * 该接口用于获取借阅人聚合根
 * 由基础设施层实现
 */
public interface Patrons {

    Option<Patron> findBy(PatronId patronId);

    // 发布领域事件
    Patron publish(PatronEvent event);
}
