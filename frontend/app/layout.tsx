import type { Metadata } from "next";
import { Manrope, Syne } from "next/font/google";
import { AuthProvider } from "@/components/AuthProvider";
// @ts-ignore
import "./globals.css";

const syne = Syne({
  variable: "--font-syne",
  subsets: ["latin"],
  weight: ["600", "700", "800"],
});

const manrope = Manrope({
  variable: "--font-manrope",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
});

export const metadata: Metadata = {
  title: "TalentPulse AI",
  description:
    "Enterprise recruitment intelligence — fit scores, skill gaps, and interview guidance. Recruiters decide.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${syne.variable} ${manrope.variable} antialiased`}>
        <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
