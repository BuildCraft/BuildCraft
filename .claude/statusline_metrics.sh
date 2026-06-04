declare -A METRICS

errors=$(cat /tmp/bc_compile_errors 2>/dev/null)
stubs=$(cat /tmp/bc_stubs 2>/dev/null)

R="\033[31m" Y="\033[33m" G="\033[32m" RESET="\033[0m"

if [ -n "$errors" ]; then
    err_c=$G
    [ "${errors:-0}" -gt 500 ] 2>/dev/null && err_c=$R || { [ "${errors:-0}" -gt 0 ] 2>/dev/null && err_c=$Y; }
    METRICS["err"]="${err_c}${errors}${RESET}"
fi

if [ -n "$stubs" ]; then
    stub_c=$Y
    [ "${stubs:-1}" = "0" ] 2>/dev/null && stub_c=$G
    METRICS["stubs"]="${stub_c}${stubs}${RESET}"
fi
