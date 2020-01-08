package io.pillopl.library.lending.patron.model;


import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import static io.pillopl.library.commons.events.EitherResult.announceFailure;
import static io.pillopl.library.commons.events.EitherResult.announceSuccess;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut.bookCheckedOutNow;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed.bookCheckingOutFailedNow;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled.holdCanceledNow;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCancelingFailed.holdCancelingFailedNow;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed.bookHoldFailedNow;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold.bookPlacedOnHoldNow;
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents.events;
import static io.pillopl.library.lending.patron.model.PatronHolds.MAX_NUMBER_OF_HOLDS;
import static io.pillopl.library.lending.patron.model.Rejection.withReason;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(of = "patron")
public class Patron {

    @NonNull
    private final PatronInformation patron;

    @NonNull
    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    @NonNull
    private final OverdueCheckouts overdueCheckouts;

    @NonNull
    private final PatronHolds patronHolds;

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook book) {
        return placeOnHold(book, HoldDuration.openEnded());
    }


    /**
     * 借阅人借阅书籍
     *
     * @param aBook    可用书籍
     * @param duration 借阅时间
     * @return
     */
    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook aBook, HoldDuration duration) {
        // 校验借阅规则
        Option<Rejection> rejection = patronCanHold(aBook, duration);
        if (rejection.isEmpty()) {
            // 构造相关领域对象
            BookPlacedOnHold bookPlacedOnHold = bookPlacedOnHoldNow(aBook.getBookId(), aBook.type(), aBook.getLibraryBranch(), patron.getPatronId(), duration);

            // 返回领域事件
            if (patronHolds.maximumHoldsAfterHolding(aBook)) {
                return announceSuccess(events(bookPlacedOnHold, MaximumNumberOhHoldsReached.now(patron, MAX_NUMBER_OF_HOLDS)));
            }
            return announceSuccess(events(bookPlacedOnHold));
        }
        return announceFailure(bookHoldFailedNow(rejection.get(), aBook.getBookId(), aBook.getLibraryBranch(), patron));
    }


    public Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold(BookOnHold book) {
        if (patronHolds.a(book)) {
            return announceSuccess(holdCanceledNow(book.getBookId(), book.getHoldPlacedAt(), patron.getPatronId()));
        }
        return announceFailure(holdCancelingFailedNow(book.getBookId(), book.getHoldPlacedAt(), patron.getPatronId()));
    }


    /**
     * 借阅人借阅提取
     *
     * @param book     已借阅的书籍
     * @param duration 提取时间
     * @return
     */
    public Either<BookCheckingOutFailed, BookCheckedOut> checkOut(BookOnHold book, CheckoutDuration duration) {
        // 其实在这里就反映出一个东西：
        // 当聚合根被提取出来的时候，属于该聚合根的所有要素都会被提取出来，因此就要处理聚合过大的问题
        if (patronHolds.a(book)) {
            return announceSuccess(bookCheckedOutNow(book.getBookId(), book.type(), book.getHoldPlacedAt(), patron.getPatronId(), duration));
        }
        return announceFailure(bookCheckingOutFailedNow(withReason("book is not on hold by patron"), book.getBookId(), book.getHoldPlacedAt(), patron));
    }


    /**
     * 借阅人是否可以进行借阅
     *
     * @param aBook
     * @param forDuration
     * @return
     */
    private Option<Rejection> patronCanHold(AvailableBook aBook, HoldDuration forDuration) {
        return placingOnHoldPolicies
                .toStream()
                .map(policy -> policy.apply(aBook, this, forDuration))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    boolean isRegular() {
        return patron.isRegular();
    }

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    public int numberOfHolds() {
        return patronHolds.count();
    }


}


