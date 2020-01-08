package io.pillopl.library.catalogue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

/**
 * 区分了Book和BookInstance
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class BookInstance {

    @NonNull
    ISBN bookIsbn;
    @NonNull
    BookId bookId;
    @NonNull
    BookType bookType;


    static BookInstance instanceOf(Book book, BookType bookType) {
        // 这里其实有体现一个业务意义：
        // 书籍是否受限是针对于BookInstance，而不是Book
        return new BookInstance(book.getBookIsbn(), new BookId(UUID.randomUUID()), bookType);
    }
}
