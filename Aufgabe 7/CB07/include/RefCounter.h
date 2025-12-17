#ifndef REFCOUNTER_H
#define REFCOUNTER_H

class RefCounter {
public:
    RefCounter();

    void inc();
    void dec();
    bool isZero() const;

private:
    unsigned int n;
};

#endif