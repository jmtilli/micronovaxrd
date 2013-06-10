% This is the main fitting code. It is called
% ctx = fitDE(ctx)
% where ctx is the fitting context provided by fitDE_init

% One call iterates the algorithm only once. A complete fit requires many
% iterations, the number of which depends on the dimensionality and complexity
% of the fitness function.

% You can get fitnesses and parameters of the best individual and the median
% individual by calling the functions fitDE_best, fitDE_best_fitness,
% fitDE_median and fitDE_median_fitness. Comparing the fitnesses of the best
% individual and the median individual is helpful in determining when to stop
% fitting.

function ctx = fitDE(ctx)
  % sampled differences to use in mutation
  %ai = randint(ctx.npop,1,ctx.npop)+1;
  %bi = randint(ctx.npop,1,ctx.npop)+1;
  %ci = randint(ctx.npop,1,ctx.npop)+1;
  ai = floor(rand(ctx.npop,1)*ctx.npop)+1;
  bi = floor(rand(ctx.npop,1)*ctx.npop)+1;
  ci = floor(rand(ctx.npop,1)*ctx.npop)+1;



  pa = ctx.pop(ai,:);
  pb = ctx.pop(bi,:);
  pc = ctx.pop(ci,:);

  % best individual
  b = ctx.pop(1,:);
  % best individual repeated npop times
  bm = ones(ctx.npop,1)*b;
  % move a list of random individuals towards the best one by ctx.lambda
  mm = pc + (bm-pc)*ctx.lambda;

  % mutate the best individual npop times by sampled differences scaled by the
  % mutation constant
  b2m = mm + ctx.km*(pa-pb);

  %assert(((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) > -0.001) & ((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) < 1.001));

  % In CovDE we rotate coordinates during the recombination phase
  if ctx.cov_on
    % Normalize parameters to range [0,1]. Due to the simplicity of DE,
    % normalization is not normally necessary. In order to calculate the
    % covariance matrix accurately, the parameters must not differ by too many
    % orders of magnitude, so in this case we have to normalize the parameters.
    ctx.pop = (ctx.pop - ctx.p_min_m) ./ (ctx.dp_m);
    ctx.pop(find(not(finite(ctx.pop)))) = 0;

    b2m = (b2m - ctx.p_min_m) ./ (ctx.dp_m);
    b2m(find(not(finite(b2m)))) = 0;

    [T,lambda] = eig(cov(ctx.pop));
    % Here cov(ctx.pop*T) is a diagonal matrix. Since the covariance matrix is
    % symmetric, T is orthogonal, which means that inv(T) == T'

    ctx.pop = ctx.pop * T;
    b2m = b2m * T;

    %assert(((ctx.pop*T' > -0.001) & (ctx.pop*T' < 1.001)) + 0);
  end


  % Recombination: mate every individual with a different mutated copy of the
  % best individual.
  % The crossover operator chooses each parameter randomly from the old
  % population and b2m. The crossover constant cr is used as weight.
  % Higher crossover constant means higher probability of getting a parameter
  % from the mutated individual.
  choices = rand(ctx.npop, ctx.nparam) < ctx.cr;
  % new trial population
  pop2 = not(choices).*ctx.pop + choices.*b2m;

  % Rotate the coordinates back and denormalize them
  if ctx.cov_on
    ctx.pop = ctx.pop * T';
    ctx.pop = ctx.p_min_m + ctx.pop.*ctx.dp_m;
    pop2 = pop2 * T';
    pop2 = ctx.p_min_m + pop2.*ctx.dp_m;
  end

  %assert(((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) > -0.001) & ((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) < 1.001) + 0 + 0);

  % replace all the parameters that are outside the limits by random values
  outside = ((pop2 < ctx.p_min_m) | (pop2 > ctx.p_max_m));
  pop2 = outside.*[ctx.p_min_m + ctx.dp_m.*rand(ctx.npop,ctx.nparam)] + not(outside).*pop2;

  %assert(((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) > -0.001) & ((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) < 1.001) + 0 + 0 + 0);

  % evaluate fitnesses for new trial population
  E2 = feval(ctx.fitnessfunc, pop2, ctx.q);

  % new population: each individual is compared to it's child and the best is selected
  newchoices = (E2<ctx.E)*ones(1,ctx.nparam);
  ctx.pop = newchoices.*pop2 + not(newchoices).*ctx.pop;
  ctx.E = min(ctx.E, E2);

  %assert(((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) > -0.001) & ((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) < 1.001) + 0 + 0 + 0 + 0);
  %ctx.E - feval(ctx.fitnessfunc, ctx.pop, ctx.q)
  %assert(all(all(abs(ctx.E - feval(ctx.fitnessfunc, ctx.pop, ctx.q))<1e-8)));

  % sort by fitness
  [ctx.E,indices] = sort(ctx.E);
  ctx.pop = ctx.pop(indices,:);

  %assert(((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) > -0.001) & ((ctx.pop - ctx.p_min_m)./(ctx.dp_m+1e-70) < 1.001) + 0 + 0 + 0 + 0 + 0);
  %assert(all(all(abs(ctx.E - feval(ctx.fitnessfunc,ctx.pop, ctx.q))<1.0e-8)));

  ctx.cycle = ctx.cycle + 1;
end
