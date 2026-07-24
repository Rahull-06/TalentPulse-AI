"use client";

import { useState } from "react";

type PasswordInputProps = {
  id?: string;
  label?: string;
  value: string;
  onChange: (value: string) => void;
  autoComplete?: string;
  required?: boolean;
  minLength?: number;
  placeholder?: string;
};

export function PasswordInput({
  id = "password",
  label = "Password",
  value,
  onChange,
  autoComplete = "current-password",
  required = true,
  minLength = 8,
  placeholder,
}: PasswordInputProps) {
  const [visible, setVisible] = useState(false);

  return (
    <label className="tp-field">
      <span className="tp-label">{label}</span>
      <div className="tp-password-wrap">
        <input
          id={id}
          className="tp-input tp-input-password"
          type={visible ? "text" : "password"}
          autoComplete={autoComplete}
          required={required}
          minLength={minLength}
          value={value}
          placeholder={placeholder}
          onChange={(e) => onChange(e.target.value)}
        />
        <button
          type="button"
          className="tp-password-toggle"
          aria-label={visible ? "Hide password" : "Show password"}
          onClick={() => setVisible((v) => !v)}
        >
          {visible ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M3 3l18 18M10.6 10.7a2.5 2.5 0 003.5 3.5M9.9 5.1A9.8 9.8 0 0112 5c5 0 9.3 3.1 11 7.5a11.4 11.4 0 01-4.1 4.9M6.1 6.2A11.3 11.3 0 001 12.5C2.7 16.9 7 20 12 20c1.7 0 3.3-.3 4.7-1"
                stroke="currentColor"
                strokeWidth="1.7"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          ) : (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M1 12.5C2.7 8.1 7 5 12 5s9.3 3.1 11 7.5C21.3 16.9 17 20 12 20S2.7 16.9 1 12.5z"
                stroke="currentColor"
                strokeWidth="1.7"
                strokeLinejoin="round"
              />
              <circle cx="12" cy="12.5" r="3" stroke="currentColor" strokeWidth="1.7" />
            </svg>
          )}
        </button>
      </div>
    </label>
  );
}
