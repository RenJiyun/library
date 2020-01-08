package io.pillopl.library.lending.patron.application.hold;


import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindBookOnHold {

    /**
     * 获取借阅延期的书籍
     *
     * @param bookId
     * @param patronId
     * @return
     */
    Option<BookOnHold> findBookOnHold(BookId bookId, PatronId patronId);
}
