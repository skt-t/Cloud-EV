export function handleApiError(err) {
    if (!err.response) {
        return "No se pudo conectar con el servidor. Verifica tu conexión.";
    }

    const { status, data } = err.response;
    const message = typeof data === 'string' ? data : (data?.message || data?.error || '');

    switch (status) {
        case 400:
            return message || "Revisa los datos ingresados.";
        case 401:
            localStorage.clear();
            window.location.href = '/';
            return "Sesión expirada. Por favor, inicia sesión nuevamente.";
        case 409:
            if (message.includes('RUT') || message === 'RUT_DUPLICADO') {
                return "Ese RUT ya está registrado";
            }
            if (message.includes('GMAIL') || message.includes('correo') || message === 'GMAIL_DUPLICADO') {
                return "Ese correo ya está registrado";
            }
            return message || "El registro ya existe (duplicado).";
        case 500:
        case 502:
            return "No se pudo completar la operación, intenta nuevamente";
        default:
            return message || "Ocurrió un error inesperado.";
    }
}
