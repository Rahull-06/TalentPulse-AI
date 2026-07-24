import { SiteHeader } from "@/components/SiteHeader";

export function AppShell({
  children,
}: {
  children: React.ReactNode;
  wide?: boolean;
}) {
  return (
    <div className="min-h-screen">
      <SiteHeader />
      <main className="tp-container py-6 sm:py-10 md:py-12">{children}</main>
    </div>
  );
}
