import { MobileTabBar } from "@/components/MobileTabBar";
import { SiteHeader } from "@/components/SiteHeader";

export function AppShell({
  children,
}: {
  children: React.ReactNode;
  wide?: boolean;
}) {
  return (
    <div className="tp-app">
      <SiteHeader />
      <main className="tp-container tp-main">{children}</main>
      <MobileTabBar />
    </div>
  );
}
