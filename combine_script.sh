find . -path './.git' -prune -o -type f -print | while read -r file; do
    echo "========================================" >> ../combined.txt
    echo "FILE: $file" >> ../combined.txt
    echo "========================================" >> ../combined.txt
    cat "$file" >> ../combined.txt
    echo -e "\n" >> ../combined.txt
done
