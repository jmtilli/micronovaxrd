function y = fitDE_median(ctx)
  y = ctx.pop(round(ctx.npop/2),:);
end
