% This function initializes the fitting context.

% fitnessfunc is a function, which takes two arguments. The first argument is
% an mxn matrix, the m rows of which are 1xn parameter vectors. The second
% argument is q, which can be anything. q is provided by the caller of
% fitDE_init. The fitness function evaluates the fitness of each m individuals
% and returns their fitnesses in an mx1 vector. Lower value means better
% fitness. 

% p_min and p_max together give the allowed values of the parameters. They must
% be 1xn vectors. Individuals are represented by 1xn vectors, the componential
% values of which must be between the respective values in p_min and p_max.

% p is an initial guess, which is an 1xn vector.

% q is the argument which is given to fitnessfunc. Check the documentation of
% the fitness function you use for more information about q.

% algoname must be one of the following:
% - 'CovDE': use eigenvalues of the covariance matrix to rotate coordinates
% - 'DE': no coordinate rotation is performed

% npop is the number of individuals in population. It should be something between
% 3 and 10 times the effective number of parameters to fit. If the value range
% of a certain parameter is a singleton set, it should not be included in the
% effective number of parameters used in calculating npop.

% the return value is the fitting context, which is given to fitDE.

function ctx = fitDE_init(fitnessfunc, p_min, p_max, p, q, algoname, npop)
  % mutation strength
  ctx.km = 0.7;
  % crossover probability
  ctx.cr = 0.5;
  % movement constant
  ctx.lambda = 1.0;

  ctx.cycle = 0;
  ctx.fitnessfunc = fitnessfunc;

  ctx.cov_on = 0;
  if strcmp(algoname, 'CovDE')
    ctx.cov_on = 1;
  end

  ctx.nparam = length(p);
  %ctx.npop = 10*ctx.nparam;
  ctx.npop = npop;

  ctx.p_min = p_min;
  ctx.p_max = p_max;
  ctx.dp = p_max - p_min;

  ctx.p_min_m = ones(ctx.npop,1)*p_min;
  ctx.p_max_m = ones(ctx.npop,1)*p_max;
  ctx.dp_m = ones(ctx.npop,1)*ctx.dp;

  ctx.pop = [ctx.p_min_m + ctx.dp_m.*rand(ctx.npop,ctx.nparam)];
  ctx.pop(1,:) = p;

  %printf('foo\n');
  %feval(ctx.fitnessfunc, p, q)

  ctx.E = feval(ctx.fitnessfunc, ctx.pop, q);
  %ctx.E(1)

  % sort by fitness
  [ctx.E,indices] = sort(ctx.E);
  ctx.pop = ctx.pop(indices,:);

  ctx.q = q;
end
