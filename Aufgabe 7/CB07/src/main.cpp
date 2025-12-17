#include <iostream>
#include <vector>
#include "Tokenizer.h"
#include "RingBuffer.h"

void A7_1_Token() {
    Token t("hello", 1, 1);
    std::cout << t.getLexem() << std::endl;
}

void A7_2_Tokenizer() {
    std::vector<Token> tokens;
    tokenize("das ist ein test", tokens);

    for (const auto& t : tokens) {
        std::cout << t.getLexem() << std::endl;
    }
}

void A7_3_RefCounter() {
    RefCounter rc;
    rc.inc();
    rc.dec();
    std::cout << rc.isZero() << std::endl;
}

void A7_4_SmartToken() {
    SmartToken a(new Token("foo", 1, 1));
    SmartToken b = a;

    std::cout << a->getLexem() << std::endl;
    std::cout << b->getLexem() << std::endl;
}

void A7_5_RingBuffer() {
    RingBuffer buf(2);

    SmartToken a(new Token("eins", 1, 1));
    SmartToken b(new Token("zwei", 1, 5));
    SmartToken c(new Token("drei", 1, 9));

    buf.writeBuffer(a);
    buf.writeBuffer(b);
    buf.writeBuffer(c); // überschreibt "one"

    SmartToken r1 = buf.readBuffer();
    SmartToken r2 = buf.readBuffer();

    std::cout << r1->getLexem() << std::endl;
    std::cout << r2->getLexem() << std::endl;
}

int main() {
    std::cout << "A7.1\n";
    A7_1_Token();

    std::cout << "A7.2\n";
    A7_2_Tokenizer();

    std::cout << "A7.3\n";
    A7_3_RefCounter();

    std::cout << "A7.4\n";
    A7_4_SmartToken();

    std::cout << "A7.5\n";
    A7_5_RingBuffer();

    return 0;
}