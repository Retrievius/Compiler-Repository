#ifndef TOKEN_H
#define TOKEN_H

class Token {
public:
    // erzeugt ein Token und kopiert den übergebenen Text
    Token(const char* l, int r, int c);

    // Copy-Konstruktor (wichtig für vector<Token>)
    Token(const Token& other);

    // Zuweisungsoperator
    Token& operator=(const Token& other);

    // räumt den reservierten Speicher wieder auf
    ~Token();

    // kleine Helfer zum Testen
    const char* getLexem() const;
    int getRow() const;
    int getCol() const;

private:
    char* lexem;
    int row;
    int col;

    // Hilfsfunktion, um Code-Duplikate zu vermeiden
    void copyLexem(const char* src);
};

#endif