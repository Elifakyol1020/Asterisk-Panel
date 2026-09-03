export interface Field {
    key: string;
    label: string;
    type?: 'text' | 'email' | 'password' | 'number' | 'select' | 'checkbox' | 'textarea';
    options?: string[];
    required?: boolean;
    min?: number;
    max?: number;
    maxLength?: number;
    minLength?: number;
    pattern?: string;
    default?: string | number | boolean;
    hint?: string;
}
export interface ResourceConfig {
    key: string;
    title: string;
    singular: string;
    description: string;
    icon: string;
    api: string;
    primary: string;
    columns: {
        key: string;
        label: string;
    }[];
    fields: Field[];
    softDelete?: boolean;
}
