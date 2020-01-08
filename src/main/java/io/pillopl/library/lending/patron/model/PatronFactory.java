package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.Tuple2;

import java.util.Map;
import java.util.Set;

import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.allCurrentPolicies;
import static java.util.stream.Collectors.toSet;

public class PatronFactory {

    /**
     * 创建借阅人
     *
     * @param patronType       借阅人类型
     * @param patronId         借阅人id
     * @param patronHolds      当前持有
     * @param overdueCheckouts 过期持有
     * @return
     */
    public Patron create(PatronType patronType, PatronId patronId, Set<Tuple2<BookId, LibraryBranchId>> patronHolds, Map<LibraryBranchId, Set<BookId>> overdueCheckouts) {
        return new Patron(
                new PatronInformation(patronId, patronType),
                allCurrentPolicies(),
                new OverdueCheckouts(overdueCheckouts),
                new PatronHolds(
                        patronHolds
                                .stream()
                                .map(tuple -> new Hold(tuple._1, tuple._2))
                                .collect(toSet()))
        );
    }

}
