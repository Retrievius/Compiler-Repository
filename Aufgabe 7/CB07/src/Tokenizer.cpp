#include "Tokenizer.h"
#include <cctype>

void tokenize(const std::string& input, std::vector<Token>& tokens) {
    std::string current;

    for (char ch : input) {
        if (std::isspace(static_cast<unsigned char>(ch))) {
            if (!current.empty()) {
                tokens.emplace_back(current.c_str(), 0, 0);
                current.clear();
            }
        } else {
            current += ch;
        }
    }

    // letztes Wort mitnehmen, falls der String nicht mit Leerzeichen endet
    if (!current.empty()) {
        tokens.emplace_back(current.c_str(), 0, 0);
    }
}