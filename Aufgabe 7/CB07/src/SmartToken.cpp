#include "SmartToken.h"

SmartToken::SmartToken(Token* p) : pObj(p), rc(nullptr) {
    if (pObj) {
        rc = new RefCounter();
    }
}

SmartToken::SmartToken(const SmartToken& sp)
    : pObj(sp.pObj), rc(sp.rc) {
    if (rc) {
        rc->inc();
    }
}

SmartToken::~SmartToken() {
    release();
}

SmartToken& SmartToken::operator=(const SmartToken& sp) {
    if (this == &sp) {
        return *this; // Selbstzuweisung ignorieren
    }

    release();

    pObj = sp.pObj;
    rc   = sp.rc;

    if (rc) {
        rc->inc();
    }

    return *this;
}

void SmartToken::release() {
    if (rc) {
        rc->dec();
        if (rc->isZero()) {
            delete pObj;
            delete rc;
        }
    }

    pObj = nullptr;
    rc   = nullptr;
}

Token& SmartToken::operator*() {
    return *pObj;
}

Token* SmartToken::operator->() {
    return pObj;
}

bool SmartToken::operator==(const SmartToken& sp) const {
    return pObj == sp.pObj;
}