#include "RefCounter.h"

RefCounter::RefCounter() : n(1) {
    // startet bei 1, weil der erste SmartToken Besitzer ist
}

void RefCounter::inc() {
    ++n;
}

void RefCounter::dec() {
    if (n > 0) {
        --n;
    }
}

bool RefCounter::isZero() const {
    return n == 0;
}