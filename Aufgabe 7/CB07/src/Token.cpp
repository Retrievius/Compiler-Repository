#include "Token.h"
#include <cstring>

// kopiert einen C-String sauber auf den Heap
void Token::copyLexem(const char* src) {
    if (!src) {
        lexem = nullptr;
        return;
    }

    size_t len = std::strlen(src);
    lexem = new char[len + 1];
    std::strcpy(lexem, src);
}

// normaler Konstruktor
Token::Token(const char* l, int r, int c)
    : lexem(nullptr), row(r), col(c)
{
    copyLexem(l);
}

// Copy-Konstruktor (tiefe Kopie!)
Token::Token(const Token& other)
    : lexem(nullptr), row(other.row), col(other.col)
{
    copyLexem(other.lexem);
}

// Zuweisungsoperator
Token& Token::operator=(const Token& other) {
    if (this == &other)
        return *this;

    delete[] lexem;              // alte Daten weg
    row = other.row;
    col = other.col;
    copyLexem(other.lexem);

    return *this;
}

// Destruktor
Token::~Token() {
    delete[] lexem;
}

const char* Token::getLexem() const { return lexem; }
int Token::getRow() const { return row; }
int Token::getCol() const { return col; }