public static Session getClientSession(String hostAddr) {
    if(REGISTRY.containsKey(hostAddr)) {
        return REGISTRY.get(hostAddr);
    } else {
        Cluster.Builder clientClusterBuilder = new Cluster.Builder().addContactPoint(hostAddr)
                .withQueryOptions(new QueryOptions()
                        .setConsistencyLevel(ConsistencyLevel.ONE)
                        .setSerialConsistencyLevel(ConsistencyLevel.LOCAL_SERIAL))
                .withoutJMXReporting()
                .withoutMetrics()
                .withReconnectionPolicy(new ConstantReconnectionPolicy(RECONNECT_DELAY_IN_MS));
        long startTimeInMillis = System.currentTimeMillis();
        Cluster clientCluster = clientClusterBuilder.build();
        Session clientSession = clientCluster.connect();

        LOG.info("Client session established after {} ms.", System.currentTimeMillis() - startTimeInMillis);
        REGISTRY.putIfAbsent(hostAddr, clientSession);
        return clientSession;
    }
}