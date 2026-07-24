export function EmptyState({
  title,
  body,
  action,
}: {
  title: string;
  body: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="tp-panel-quiet px-6 py-14 text-center sm:px-10">
      <h2 className="tp-display text-[1.65rem] tracking-tight text-[var(--ink)]">{title}</h2>
      <p className="tp-muted mx-auto mt-2.5 max-w-md text-sm leading-relaxed">{body}</p>
      {action ? <div className="mt-7 flex justify-center">{action}</div> : null}
    </div>
  );
}
