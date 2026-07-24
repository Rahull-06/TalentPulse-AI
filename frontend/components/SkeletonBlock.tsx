export function SkeletonBlock({ lines = 3 }: { lines?: number }) {
  return (
    <div className="tp-panel p-5 sm:p-6" aria-hidden>
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          className="tp-skeleton tp-skeleton-line"
          style={{ width: i === lines - 1 ? "58%" : "100%" }}
        />
      ))}
    </div>
  );
}
